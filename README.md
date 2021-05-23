# Inventory Management
A Generic Inventory Management platform

## Problem Statement
Talking of inventory management, one of the prospective high value customers of Collect recently came to us with a very interesting use case. They work in the mining sector, and have thousands of trucks for moving minerals from extraction site to refinery, and finally to their warehouse.  The above journey from and back to extraction site generally takes a week of time. New trucks keep getting registered everyday as well. The customer used to maintain this with entry/exit registers at different checkpoints, but now wants to make their truck movement more efficient by collecting high quality data, and then have near real-time monitoring the data in Google Sheets, from where they are powering multiple dashboards.

The  important checkpoints of book-keeping to the customer fall across the following :
1. Truck Registration
2. Site Entry
3. Site Exit
4. Refinery Entry
5. Refinery Exit
6. Warehouse Entry
7. Warehouse Exit

Collect already has a built in monitoring question type, wherein an entity can be registered, and then monitored several times. This question type just shows already registered entities as a list of choices. Read more about monitoring here .The customer also wants to enforce the following constraint in order to achieve high data quality, towards which we need to build further :

1. A new truck registered in checkpoint 1 should only start showing up as available in list of trucks in checkpoint 2 and no other checkpoints
2. A truck should only show up as a selectable option in the exit register of any facility ( for any of the site, refinery and warehouse ) if it’s last entry is in the entry register for the facility
3. A truck should only be eligible to enter into the next facility only if it has left the  previous facility
4. A truck should not take more than one round-trip within a week, i.e., it should not show up as a selectable option in the `Site-Entry` log-book if it’s last entry was less than a week ago.

#### Further Details
We preempt that with time, more similar use cases will arise, with different “actions” being required once the response hits the primary store/database. We want to solve this problem in such a way that each new use case can just be “plugged in” and does not need an overhaul on the backend. Imagine this as a whole ecosystem for integrations. We want to optimise for latency and having a unified interface acting as a middleman.

Designing a sample schematic for how we would store forms (with questions) and responses (with answers) in the Collect data store. Forms, Questions, Responses and Answers each will have relevant metadata. Designing and implementing a solution for the Google Sheets use case and keeping in mind on how we want to solve this problem in a plug-n-play fashion. Making assumptions wherever possible.

Eventual consistency is what the clients expect as an outcome of this feature, making sure no responses get missed in the journey. Do keep in mind that this solution must be failsafe, should eventually recover from circumstances like power / internet / service outages, and should scale to cases like millions of responses across hundreds of forms for an organization.

There are points for details on how would you benchmark, set up logs, monitor for system health and alerts for when the system health is affected for both the cloud as well as bare-metal. Read up on if there are limitations on the third party ( Google sheets in this case ) too, a good solution keeps in mind that too.

## Solution

Technology to be used:
1. Cassandra/ Ignite (High fault tolerant homogeneous environment) - data storage for team management, questions, responses and other metadata used by the system
2. Kafka (Real time streaming messaging queue) - used for data flow within the system
3. JAVA - Dropwizard

### High Level Architecture

I have divided the data collection platform into 2 blocks: Ingestion Block, Engine/ Processing Block

#### Data Flow

Step 1: Interaction with Interface
The console will provide an interface for authorized teams to create forms where team members can fill up.

Step 2: Data Ingestion
The JSON data will hit the Collect Ingestor API which will internally push the data to Kafka if it is a valid and an authorized POST request.
For now:
Create question requests will directly get stored in the DB
Responses will flow through kafka and will be stored in the DB by the Engine
Will have monitoring APIs

Step 3: Data Collection
Collect Engine will pull the data from Kafka and push data to DB after transformations if required. Collect Engine includes integration with 3rd Party APIs and processing if required.

Note: I have used Ignite as the DB for the implementation

### Low Level Architecture

Ingestor Service

Has 4 APIs

1. Create Form API which has id and ts as specific fields. formContent is a generic field which can take any type of key value pairs. This is made generic in order to incorporate any kind of forms which when requested can be rendered in the UI.
   The data directly gets stored in the DB as this API will be hit less when compared to the response API.
   If this API usage is more, we can push this data to Kafka
   If the DB operation fails, it shows in the UI and the form can be created again.

2. Submit Form API which is basically form submission. Here type is a mandatory field which basically represents the table name or the the type of the form submitted. Submission is a genric key value pair which should ideally adhere to the table schema.
   If the kafka operation fails, currently we are sending an internal server response.

3. Checkpoint API which gives the list of trucks available for entry in the next checkpoint.

4. CanStart API which gives A truck should not take more than one round-trip within a week, i.e., it should not show up as a selectable option in the `Site-Entry` log-book if it’s last entry was less than a week ago. This API gives the list of trucks who are available to start a new trip

Apart from these API’s we can have API’s to update the form, and other API’s which could expose much more analysis and monitoring of the current scenario.

### Working

Engine reads from kafka and inserts into the DB dynamically generating the query depending on the type of the data.

The table structure contains an extra column called as Sync which is by default false. This value basically states that whether it is synced to Google Sheets or not. Whenever it is synced to google sheet, it is updated as true. If for any reason, it is not able to update the google sheet, it does not update the value and hence gets re-tried after 100 seconds.
Google Sheet API has a limit of 100 requests in 100 seconds per user. So, to overcome this, I am throttling the updates done in the Google Sheet. There is a GoogleSheetSyncScheduler class which runs every 100 seconds, picks up 100 not synced records from the DB. Appends/ Updates the google sheet and then updates the DB with sync as true.

#### Plug - in architecture:

Let’s say, if we have a similar use case but for a different domain, there will be no code changes required. The changes we need to do:
1. Provide the table schema in the Engine configuration file
2. Insert the Create DDL command for the table structure
3. Define the checkpoint order in the Ingestor configuration file

PS: there are certain limitations as far the table structure and the API JSON is concerned.
1. The table must have an id and sync column field
2. The create form must have an id and TS
3. The submit form must have type and submissions field

#### Scalability and High Availability
The Ingestor and Engine services can be spawned as many as required. It is vertically scalable. If one ingestor goes down, the other ingestor can take up the load and same for engine service.

#### Caveats in design:
If the DB operation fails for submission insertion, we are not doing anything. Instead we can have a separate DB or a different kafka to insert all the failed records. Same if the Kafka operation fails.
The Engine Scheduler needs to be fast enough. THere could be scenarios where the number of submissions could be piled up since the google sheet has a sync up rate of 1 request per second.

Monitoring of the ecosystem can be done by prometheus. Alerting could be set up based on the prometheus values. Also latencies has to be tracked for each operation to understand what can be improved. Fetching latencies also helps to benchmark the application.

Logs are a necessary item in such applications. It should be there to unravel any inconsistencies happening within the system.

##### Incomplete:
Google Sheet integration to the project. I was able to append and update the values from the JAVA code. The commented code works perfectly fine but was unable to integrate because of the jetty-util version. Will be looking into it in further upgrades.
