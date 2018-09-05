<?php require __DIR__ . '/../../../lib/php/env_helper.inc.php'; ?>
<dataConfig>
<dataSource name="ds1" driver="org.postgresql.Driver"
            url="jdbc:postgresql://<?= $host ?>:<?= $_ENV['PGPORT'] ?>/<?= $_ENV['PGDATABASE'] ?>"
            user="biomart_user" password="<?= htmlspecialchars($biomart_user_pwd) ?>"
            readOnly="true" autoCommit="false" />
    <document>
        <entity name="Samples" query="select sample_id,trial_name,field1,field2,field3,field4,field5,field6,field7,field8,field9,field10,tissue_type,data_types,disease,tissue_state,biobank,treatment,sample_treatment,source_organism,timepoint from i2b2DemoData.sample_categories">
            <field name="id" column="sample_id"/>

	    <field name="trial_name" column="trial_name"/>
            <field name="sample_id" column="sample_id"/>

	    <field name="barcode" column="field1"/>
            <field name="plate_id" column="field2"/>
            <field name="patient_id" column="field3"/>
            <field name="external_id" column="field4"/>
            <field name="aliquot_id" column="field5"/>
            <field name="visit" column="field6"/>
            <field name="sample_type" column="field7"/>
            <field name="description" column="field8"/>
            <field name="comment" column="field9"/>
	    <field name="location" column="field10"/>

<!--
	    <field name="field11" column="field11"/>
	    <field name="field12" column="field12"/>
	    <field name="field13" column="field13"/>
	    <field name="field14" column="field14"/>
	    <field name="field15" column="field15"/>
-->
	    <field name="tissue_type" column="tissue_type"/>
	    <field name="data_types" column="data_types"/>
	    <field name="disease" column="disease"/>
	    <field name="tissue_state" column="tissue_state"/>
	    <field name="biobank" column="biobank"/>
	    <field name="source_organism" column="source_organism"/>
	    <field name="treatment" column="treatment"/>
	    <field name="sample_treatment" column="sample_treatment"/>
	    <field name="subject_treatment" column="subject_treatment"/>
	    <field name="timepoint" column="timepoint"/>
        </entity>
    </document>
</dataConfig>
