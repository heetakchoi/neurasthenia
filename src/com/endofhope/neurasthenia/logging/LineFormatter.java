/**
 * Licensed to LGPL v3.
 */
package com.endofhope.neurasthenia.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.endofhope.neurasthenia.Constants;

/**
 * 
 * @author endofhope
 *
 */
public class LineFormatter extends Formatter{
	
	public LineFormatter(){
		super();
	}

	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[").append(record.getLevel().getName()).append("]");

		Date date = new Date(record.getMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("MMdd HH:mm:ss");
		String dateString = sdf.format(date);
		sb.append("[").append(dateString).append("] ");
		
		sb.append(formatMessage(record));
		Throwable t = record.getThrown();
		if(t != null){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			sb.append(Constants.cLF);
			sb.append(sw.getBuffer());
			try {
				sw.close();
			} catch (IOException e) {
				sb.append(e.getMessage());
			}
			pw.close();
		}
		String className = record.getSourceClassName();
		if(className.startsWith("com.endofhope.neurasthenia")){
			className = className.substring("com.endofhope.neurasthenia".length()+1);
		}
		sb.append(" [").append(className)
		.append(".").append(record.getSourceMethodName()).append("]");
		
		sb.append("\n");
		
		return sb.toString();
	}

}
