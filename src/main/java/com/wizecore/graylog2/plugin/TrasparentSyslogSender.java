package com.wizecore.graylog2.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.SyslogIF;

/**
 * Formats fields into message text 
 * 

        <34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8
         ^priority
         	^ version
         	  ^ date 
         	  						   ^ host
         	  						   						 ^ APP-NAME
         	  						   						 	^ structured data?
         	  						   						 	  ^ MSGID 
         	  						   						 	  	    
 */
public class TrasparentSyslogSender implements MessageSender {
	private Logger log = Logger.getLogger(TrasparentSyslogSender.class.getName());
  private boolean removeHeader = false;

	public static final String SYSLOG_DATEFORMAT = "MMM dd HH:mm:ss";

  public TrasparentSyslogSender(Configuration conf){
    removeHeader = conf.getBoolean("transparentFormatRemoveHeader");
  }
	
	/**
	 * From syslog4j
	 * 
	 * @param dt
	 * @return
	 */
	public static void appendSyslogTimestamp(Date dt, StringBuilder buffer) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(SYSLOG_DATEFORMAT,Locale.ENGLISH);		
		String datePrefix = dateFormat.format(dt);	
		
		int pos = buffer.length() + 4;		
		buffer.append(datePrefix);
	
		//  RFC 3164 requires leading space for days 1-9
		if (buffer.charAt(pos) == '0') {
			buffer.setCharAt(pos,' ');
		}
	}
	
	@Override
	public void send(SyslogIF syslog, int level, Message msg) {
		StringBuilder out = new StringBuilder();
		if(removeHeader!=true){
      appendHeader(msg, out);
    }
		
		out.append(msg.getMessage());
		String str = out.toString();
		// log.info("Sending plain message: " + level + ", " + str);
		syslog.log(level, str);
	}

	public static void appendHeader(Message msg, StringBuilder out) {
		Date dt = null;
		Object ts = msg.getField("timestamp");
		if (ts != null && ts instanceof Number) {
			dt = new Date(((Number) ts).longValue());
		}
		
		if (dt == null) {
			dt = new Date();
		}
		
		// Write time
		appendSyslogTimestamp(dt, out);
		out.append(" ");
	}
}
