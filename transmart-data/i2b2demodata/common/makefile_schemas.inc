ifdef THIS_SCHEMA
SCHEMA_COMP := '$(THIS_SCHEMA)'
else
SCHEMA_COMP := IN ('i2b2demodata', 'i2b2metadata', 'i2b2hive', 'i2b2imdata', 'i2b2pm', 'i2b2workdata')
endif
