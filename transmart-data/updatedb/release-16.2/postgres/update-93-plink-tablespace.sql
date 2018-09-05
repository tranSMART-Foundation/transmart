--
-- fix owner, tablespaces and permissions for ts_batch
-- handled automatically in initial make, processed individually here
--

set search_path = gwas_plink, pg_catalog;

ALTER SCHEMA gwas_plink OWNER TO gwas_plink;

ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA gwas_plink GRANT ALL ON TABLES TO biomart_user;
ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA gwas_plink GRANT ALL ON SEQUENCES TO biomart_user;
ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA gwas_plink GRANT ALL ON FUNCTIONS TO biomart_user;

ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA gwas_plink GRANT ALL ON TABLES TO tm_cz;
ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA gwas_plink GRANT ALL ON SEQUENCES TO tm_cz;
ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA gwas_plink GRANT ALL ON FUNCTIONS TO tm_cz;

ALTER DEFAULT PRIVILEGES FOR USER tm_cz IN SCHEMA gwas_plink GRANT ALL ON TABLES TO biomart_user;
ALTER DEFAULT PRIVILEGES FOR USER tm_cz IN SCHEMA gwas_plink GRANT ALL ON SEQUENCES TO biomart_user;
ALTER DEFAULT PRIVILEGES FOR USER tm_cz IN SCHEMA gwas_plink GRANT ALL ON FUNCTIONS TO biomart_user;

ALTER DEFAULT PRIVILEGES FOR USER tm_cz IN SCHEMA gwas_plink GRANT ALL ON TABLES TO tm_cz;
ALTER DEFAULT PRIVILEGES FOR USER tm_cz IN SCHEMA gwas_plink GRANT ALL ON SEQUENCES TO tm_cz;
ALTER DEFAULT PRIVILEGES FOR USER tm_cz IN SCHEMA gwas_plink GRANT ALL ON FUNCTIONS TO tm_cz;


ALTER TABLE plink_data OWNER TO gwas_plink, SET TABLESPACE transmart;

ALTER INDEX plink_data_pkey SET TABLESPACE indx;
ALTER INDEX plink_data_study_id_key SET TABLESPACE indx;

ALTER SEQUENCE seq_plink_data_id OWNER TO gwas_plink;

GRANT ALL ON TABLE plink_data TO tm_cz;
GRANT SELECT ON TABLE plink_data TO biomart_user;
GRANT ALL ON TABLE plink_data TO gwas_plink;

GRANT ALL ON SEQUENCE seq_plink_data_id TO tm_cz;
GRANT ALL ON SEQUENCE seq_plink_data_id TO biomart_user;
GRANT ALL ON SEQUENCE seq_plink_data_id TO gwas_plink;


