

var eng = {    
    dataStores:{},                      //DataStores
    dataSources: {},                    //Datasources
    fieldProcesors:{},                  //Procesadores de field elements
    validators:{},                      //Validator templates
    dataServices:{},                    //Servicios
    dataProcessors:{},                  //DataProcessors
    dataExtractors:{},                  //DataExtractors
    fileSources: {},
    routes:{},
    getDataSource:function()
    {
        return {
            toValueMap:function()
            {
                return null;
            }
        }
    },
    require:function(source)
    {
        var DataMgr = Java.type('org.semanticwb.datamanager.DataMgr');
        try
        {
            load(DataMgr.getApplicationPath()+source);
        }catch(e){print(e);}
    }
};







