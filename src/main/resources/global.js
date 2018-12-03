

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
    _requires:[],
    isServerSide:true,
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
            var i=source.indexOf("?");
            if(i>-1)source=source.substring(0,i);

            if(eng._requires.indexOf(source)==-1)
            {
                if(source==="/admin/admin_db.js")
                {
                    //print("build:"+source);  
                    var AdminUtils = Java.type('org.semanticwb.swbforms.admin.AdminUtils');
                    var script=AdminUtils.getDataSourceScriptFromDB(null,false);
                    //print(script);
                    eval(script);                
                }else
                {
                    //print("load:"+source);            
                    load(DataMgr.getApplicationPath()+source);
                }
                eng._requires.push(source);
                return true;
            }else
            {
                //print("noLoad:"+source);
            }
        }catch(e){print(e);}
        return false;
    }
};







