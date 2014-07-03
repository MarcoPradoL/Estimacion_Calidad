package xm.soft;

import java.net.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class ServiceInfoProvider {

    private ArrayList<Service> mServices = new ArrayList<Service>();

    private String getStringResource(String strUrl) throws ServiceProviderInfoException {
	HttpURLConnection connection = null;
	try {
	    URL url = new URL(strUrl);
	    connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("GET");
	    connection.addRequestProperty("Host", url.getHost());
	    connection.connect();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    String content = "";
	    String line;
	    while ((line = reader.readLine()) != null) {
		content += line;
	    }
	    return content;
	} catch (IOException exception) {
	    throw new ServiceProviderInfoException(exception.getMessage());
	} finally {
	    if (null != connection) {
		connection.disconnect();
	    }
	}
    }

    //Mozilla 44699/11627
    //Wikimedia 41159/8777
    //CloudMonitor 57072/20502
    public static void main(String arg[]) throws Exception {
	String urlMozilla = "http://api.io.watchmouse.com/synth/current/44699/folder/11627/?fields=info;cur;24h.uptime;24h.status;last.date;daily.avg;daily.uptime;daily.status;daily.period";
	String urlWikimedia = "http://api.io.watchmouse.com/synth/current/41159/folder/8777/?fields=info;cur;24h.uptime;24h.status;last.date;daily.avg;daily.uptime;daily.status;daily.period";
	String urlCloudMonitor = "http://api.io.watchmouse.com/synth/current/57072/folder/20502/?fields=info;cur;24h.uptime;24h.status;last.date;daily.avg;daily.uptime;daily.status;daily.period";
	ServiceInfoProvider provider = new ServiceInfoProvider(urlMozilla);
	printTest(provider);
	provider = new ServiceInfoProvider(urlWikimedia);
	printTest(provider);
	provider = new ServiceInfoProvider(urlCloudMonitor);
	printTest(provider);

    }

    public static void printTest(ServiceInfoProvider provider) {
	List<Service> services = provider.getServices();
	Collections.sort(services, new Comparator<Service>() {
	    @Override
	    public int compare(Service o1, Service o2) {
		if (o1.getName() != null) {
		    return o1.getName().compareTo(o2.getName());
		}
		return -1;
	    }
	});
	System.out.println("Service/Website\tPerformance (ms)\tAvailability(%)");
	for (Service service : services) {
	    String status = "✓";
	    if (service.getStatus() == 1) {
		status = "⚠";
	    } else if (service.getStatus() == 2) {
		status = "✗";
	    }
	    System.out.println(status + " " + service.getName() + "\t" + service.getPerformance() + "\t" + service.getAvailability());
	}
	System.out.println();
    }

    public ServiceInfoProvider(String strUrl) throws ServiceProviderInfoException {
	String content = getStringResource(strUrl);
	JSONObject data = new JSONObject(content);
	try {
	    if (data.optInt("count") > 0) {
		JSONArray results = data.getJSONArray("result");

		for (int i = 0; i < results.length(); i++) {
		    JSONObject objTemp = results.getJSONObject(i);
		    Service service = new Service();
		    service.setName(objTemp.getJSONObject("info").getString("name"));
		    service.setAvailability(objTemp.getJSONObject("24h").getDouble("uptime"));
		    service.setExtraInfo(objTemp.getJSONObject("info").getString("type"));
		    objTemp = objTemp.getJSONObject("cur");
		    service.setStatus(objTemp.getInt("status"));
		    objTemp = objTemp.getJSONObject("avg");
		    double ms = objTemp.getDouble("ttime");
		    service.setPerformance((int) (Math.round(ms)));
		    mServices.add(service);
		}
	    }
	} catch (JSONException e) {
	    throw new ServiceProviderInfoException(e.getMessage());
	}

    }

    public static class ServiceProviderInfoException extends Exception {

	private static final long serialVersionUID = 1L;

	public ServiceProviderInfoException(String message) {
	    super(message);
	}
    }

    public ArrayList<Service> getServices() {
	return mServices;
    }

    public Service getServiceByName(String name) {
	for (Service srv : mServices) {
	    if (name != null && name.equalsIgnoreCase(srv.getName())) {
		return srv;
	    }
	}
	return null;
    }
}
