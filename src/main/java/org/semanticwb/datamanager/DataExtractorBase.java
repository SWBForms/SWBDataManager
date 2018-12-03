/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.semanticwb.datamanager;

import java.io.IOException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.semanticwb.datamanager.script.ScriptObject;

/**
 *
 * @author javiersolis
 */
public interface DataExtractorBase
{

    /**
     *
     * @param data
     * @throws IOException
     */
    public void store(DataObject data) throws IOException;

    /**
     *
     * @param data
     * @throws IOException
     */
    public void store(ScriptObjectMirror data) throws IOException;

    /**
     *
     * @return
     */
    public String getName();

    /**
     *
     * @return
     */
    public SWBScriptEngine getScriptEngine();

    /**
     *
     * @return
     */
    public ScriptObject getScriptObject();

    /**
     *
     * @return
     */
    public SWBDataSource getDataSource();
}
