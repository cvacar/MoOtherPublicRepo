insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Physical Form','physicalForm','list',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Collection Date','collectionDate','datetime',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Notebook Ref','notebookReference','freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Internal ID','internalId','freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Field Name','fieldName','freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Description','description','freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Volume (L)','volume_l','numeric(4,1)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Weight (g)','weight_g','numeric(4,1)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Temperature (degrees C)','temperature_C','numeric(4,1)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('pH','pH','numeric(4,1)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Dissolved Oxygen (mg/L)','dissolvedO2_mg_L','numeric(4,1)',1)
-- todo: alter column name to conductivity_mS_cm
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Conductivity (mS/cm)','conductivity_mS_m','numeric(4,1)',1)
-- todo: alter column name to sampleDepth_m
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Sample Depth (m)','sample_depth_m','numeric(4,1)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Water column depth (m)','waterColumnDepth_m','numeric(4,1)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Salinity (ppt)','salinity_ppt','numeric(4,1)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('DO (%)','DO_pct','numeric(4,1)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('TDS (ppt)','TDS_ppt','numeric(4,2)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Storage Method','storageMethod','freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Closest Town',null,'freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('City',null,'freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('County',null,'freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('State',null,'freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Country',null,'freetype)',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Archive Location','archiveLocation','freetype',1)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Location 1 [Freezer:Box:Position]',null,'freetype',0)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Location 2 [Freezer:Box:Position]',null,'freetype',0)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Location 3 [Freezer:Box:Position]',null,'freetype',0)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Location 4 [Freezer:Box:Position]',null,'freetype',0)
insert into #tmpMapProperty
(templateField,tableField,dataFormat,optional)
values ('Comment','comment','freetype',1)