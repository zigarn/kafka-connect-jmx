package com.zigarn.kafka.connect.transforms;


import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.Map;
import org.apache.kafka.connect.errors.DataException;


public class KeyToValue<R extends ConnectRecord<R>> implements Transformation<R>
{

  public static final String OVERVIEW_DOC
      = "Update the record's value by inserting a new column with the key of the record";

  public static final ConfigDef CONFIG_DEF = new ConfigDef()
      .define(ConfigName.FIELD_NAME, ConfigDef.Type.STRING, ConfigDef.Importance.MEDIUM,
          "Field name");


  private interface ConfigName
  {
    String FIELD_NAME = KeyToValue.FIELD_NAME;
  }

  public final static String FIELD_NAME = "key.field.name";

  private static final String PURPOSE = "insert key into value struct";


  @Override
  public void configure(Map<String, ?> props)
  {
  }

  @Override
  public R apply(R record)
  {
    if (!(record.value() instanceof Struct))
      throw new DataException("Only Struct objects supported for [" + PURPOSE + "], found: " + (record.value() == null ? "null" : record.value().getClass().getName()));

    final Struct value = (Struct)record.value();

    Schema updatedSchema = makeUpdatedSchema(record.valueSchema());
    final Struct updatedValue = new Struct(updatedSchema);

    for (Field field : updatedValue.schema().fields())
    {
      if (field.name().equals(FIELD_NAME))
      {
        updatedValue.put(field.name(), record.key());
      }
      else
      {
        updatedValue.put(field.name(), value.get(field));
      }
    }
    return record.newRecord(
        record.topic(),
        record.kafkaPartition(),
        record.keySchema(),
        record.key(),
        updatedSchema,
        updatedValue,
        record.timestamp()
    );
  }

  private Schema makeUpdatedSchema(Schema schema)
  {
    final SchemaBuilder builder = SchemaBuilder.struct();

    builder.name(schema.name());
    builder.version(schema.version());
    builder.doc(schema.doc());

    final Map<String, String> params = schema.parameters();
    if (params != null)
    {
      builder.parameters(params);
    }

    for (Field field : schema.fields())
    {
      builder.field(field.name(), field.schema());
    }
    builder.field(FIELD_NAME, Schema.OPTIONAL_STRING_SCHEMA);

    return builder.build();
  }

  @Override
  public void close()
  {
  }

  @Override
  public ConfigDef config()
  {
    return CONFIG_DEF;
  }

}
