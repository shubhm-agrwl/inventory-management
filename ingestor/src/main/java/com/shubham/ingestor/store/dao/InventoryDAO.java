package com.shubham.ingestor.store.dao;

import com.shubham.ingestor.common.CreateForm;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface InventoryDAO {

  @SqlUpdate("insert into forms (id, form_content, updated_at) values (:id, :formContent,:ts)")
  void createForm(@BindBean CreateForm createForm, @Bind("formContent") String formContent);

}
