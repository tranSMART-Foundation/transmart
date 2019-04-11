--
-- Name: news_updates; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE news_updates (
    newsid int,
    ranbyuser character varying(200),
    rowsaffected int,
    operation character varying(200),
    datasetname character varying(200),
    updatedate timestamp,
    commentfield character varying(200)
);

