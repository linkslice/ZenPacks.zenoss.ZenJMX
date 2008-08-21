///////////////////////////////////////////////////////////////////////////
//
// This program is part of Zenoss Core, an open source monitoring platform.
// Copyright (C) 2007, Zenoss Inc.
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License version 2 as published by
// the Free Software Foundation.
//
// For complete information please visit: http://www.zenoss.com/oss/
//
///////////////////////////////////////////////////////////////////////////
package com.zenoss.zenpacks.zenjmx.call;

import java.io.IOException;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import static com.zenoss.zenpacks.zenjmx.call.JmxCall.*;
import static com.zenoss.zenpacks.zenjmx.call.SingleValueAttributeCall.*;
import static com.zenoss.zenpacks.zenjmx.call.CallFactory.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.zenoss.jmx.JmxClient;
import com.zenoss.jmx.JmxException;
import com.zenoss.zenpacks.zenjmx.ConfigAdapter;


/**
 * <p>
 * Call for a multi-value attribute
 * </p>
 *
 * <p>$Author: chris $</p>
 *
 * @author Christopher Blunck
 * @version $Revision: 1.6 $
 */
public class MultiValueAttributeCall
  extends JmxCall {

  // configuration parameters
  public static final String ATTRIBUTE_KEY = "attributeKey";


  // the name of the attribute to query
  private String _attrName;

  // the keys of the attributes we should query
  private List<String> _keys;

  // logger
  private static final Log _logger = 
    LogFactory.getLog(MultiValueAttributeCall.class);


  /**
   * Creates a MultiValueAttributeCall
   */
  public MultiValueAttributeCall(String objectName,
                                 String attrName,
                                 List<String> keys,
                                 List<String> types) {
    super(objectName);

    _attrName = attrName;
    _keys = keys;
    
    setTypeMap(buildTypeMap(keys, types));

    _summary.setCallSummary("multi-value attribute: " + attrName + 
                            " (" + keys + ")");
  }


  /**
   * Returns the name of the attribute being queried
   */
  public String getAttributeName() { return _attrName; }


  /**
   * Returns the sub-attribute names that are being queried.  These
   * are the fields of the multi-value attribute we are interested in.
   */
  public List<String> getKeys() { return _keys; }


  /**
   * @throws JmxException 
 * @see Callable#call
   */
  public Summary call(JmxClient client) throws JmxException {
      // record when we started
      _startTime = System.currentTimeMillis();
      // issue the query
      Map<String, Object> values = client.query(_objectName, _attrName, _keys);
      
      _summary.setResults(values);
      
      // record the runtime of the call
      _summary.setRuntime(System.currentTimeMillis() - _startTime);
      
      // set our id so the processor can remove it from the reactor
      _summary.setCallId(hashCode());
    
      // return result
      return _summary;
    
  }


  /**
   * Creates a MultiValueAttributeCall from the configuration provided
   */
  public static MultiValueAttributeCall fromValue(ConfigAdapter  config) 
    throws ConfigurationException {

    String url = Utility.getUrl(config);
    boolean auth = config.authenticate();

    // ugly form of downcasting...  but XML-RPC doesn't give us a List<String>
    List<String> keys = config.getDataPoints();
    List<String> types = config.getDataPointTypes();

    MultiValueAttributeCall call = 
      new MultiValueAttributeCall(config.getOjectName(),
                                  config.getAttributeName(),
                                  keys,
                                  types);
    
    call.setDeviceId(config.getDevice());
    call.setDataSourceId(config.getDatasourceId());

    return call;
  }


  /**
   * @see Object#equals
   */
  public boolean equals(Object other) {
    if (! (other instanceof MultiValueAttributeCall)) {
      return false;
    }

    boolean toReturn = super.equals(other);

    MultiValueAttributeCall call = (MultiValueAttributeCall) other;

    toReturn &= Utility.equals(call.getAttributeName(), getAttributeName());
    toReturn &= Utility.equals(call.getKeys(), getKeys());
    
    return toReturn;
  }
  

  /**
   * @see JmxCall#hashCode
   */
  public int hashCode() {
    int hc = 0;

    hc += super.hashCode();
    hc += hashCode(_attrName);
    hc += hashCode(_keys);

    return hc;
  }
  
}
