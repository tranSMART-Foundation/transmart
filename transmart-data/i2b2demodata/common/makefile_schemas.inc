ifdef THIS_SCHEMA
SCHEMA_COMP := '$(THIS_SCHEMA)'
else
SCHEMA_COMP := IN ('i2b2demodata', 'i2b2metadata')
endif
