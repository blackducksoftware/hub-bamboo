/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 *  
 * http://www.blackducksoftware.com/
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/

package ut.com.blackducksoftware.integration.hub.bamboo.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public class TestLogger implements IntLogger {
	private ArrayList<String> outputList = new ArrayList<String>();

	private ArrayList<Throwable> errorList = new ArrayList<Throwable>();

	public ArrayList<String> getOutputList() {
		return outputList;
	}

	public ArrayList<Throwable> getErrorList() {
		return errorList;
	}

	public void resetOutputList() {
		outputList = new ArrayList<String>();
	}

	public void resetErrorList() {
		errorList = new ArrayList<Throwable>();
	}

	public void resetAllOutput() {
		resetOutputList();
		resetErrorList();
	}

	public String getOutputString() {
		return StringUtils.join(outputList, '\n');
	}

	public String getErrorOutputString() {
		final StringBuilder sb = new StringBuilder();
		if (errorList != null && !errorList.isEmpty()) {
			for (final Throwable e : errorList) {
				if (sb.length() > 0) {
					sb.append('\n');
				}
				final StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				sb.append(sw.toString());
			}
		}
		return sb.toString();
	}

	@Override
	public void debug(final String txt) {
		outputList.add(txt);
	}

	@Override
	public void debug(final String txt, final Throwable e) {
		outputList.add(txt);
		errorList.add(e);
	}

	@Override
	public void error(final Throwable e) {
		errorList.add(e);
	}

	@Override
	public void error(final String txt) {
		outputList.add(txt);
	}

	@Override
	public void error(final String txt, final Throwable e) {
		outputList.add(txt);
		errorList.add(e);
	}

	@Override
	public void info(final String txt) {
		outputList.add(txt);
	}

	@Override
	public void trace(final String txt) {
		outputList.add(txt);
	}

	@Override
	public void trace(final String txt, final Throwable e) {
		outputList.add(txt);
		errorList.add(e);
	}

	@Override
	public void warn(final String txt) {
		outputList.add(txt);
	}

	@Override
	public void setLogLevel(final LogLevel level) {
	}

	@Override
	public LogLevel getLogLevel() {
		return null;
	}

}
