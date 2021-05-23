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