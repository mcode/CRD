package org.hl7.davinci.endpoint;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.google.common.net.HttpHeaders;

public class Utils {

  public static URL getApplicationBaseUrl(HttpServletRequest request) {
    try {
      URL url = null;

      // grab the forwarded values if they are not null
      if (request.getHeader(HttpHeaders.X_FORWARDED_HOST) != null) {
        String serverName = request.getHeader(HttpHeaders.X_FORWARDED_HOST);

        // grab the last forwarded url
        String[] serverParts = serverName.split(", ");
        serverName = serverParts[serverParts.length - 1];

        // default protocol to http if not set
        String proto = (request.getHeader(HttpHeaders.X_FORWARDED_PROTO) != null) ? request.getHeader(HttpHeaders.X_FORWARDED_PROTO) : "http";

        if (request.getHeader(HttpHeaders.X_FORWARDED_PORT) != null) {
          url = new URL(proto, serverName, Integer.parseInt(request.getHeader(HttpHeaders.X_FORWARDED_PORT)), request.getContextPath());
        } else {
          url = new URL(proto, serverName, request.getContextPath());
        }

      } else {
        url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
      }

      return url;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to get current server URL");
    }
  }

  public static String stripResourceType(String identifier) {
    int indexOfDivider = identifier.indexOf('/');
    if (indexOfDivider+1 == identifier.length()) {
      // remove the trailing '/'
      return identifier.substring(0, indexOfDivider);
    } else {
      return identifier.substring(indexOfDivider+1);
    }
  }

  public static boolean idInSelectionsList(String identifier, List<String> selections) {
    if (selections.isEmpty()) {
      return true;
    } else {
      for ( String selection : selections) {
        if (identifier.contains(stripResourceType(selection))) {
          return true;
        }
      }
      return false;
    }
  }

}
