package org.costa;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public final class App {

  private static final int MARATHON_PAGES = 30;
  private static final int HALFMARATHON_PAGES = 84;
  private static final int NO_CAT_POSITION = -1;

  private static final Pattern PATTERN = Pattern
      .compile("(<td>([a-zA-Z0-9:\\x80-\\xFF \\.'`-]+"
          + "|<strong>[a-zA-Z0-9:\\x80-\\xFF \\.'`-]+</strong>)</td>"
          + "|<td></td>)" + "|<td><strong></strong></td>");

  private static final int OPEN_TD_LENGHT = 4;
  private static final int CLOSE_TD_LENGHT = 5;
  private static final int OPEN_STRONG_LENGTH = 8;
  private static final int CLOSE_STRONG_LENGTH = 9;

  private static final String OPEN_STRONG_TAG = "<strong>";
  private static final String CLOSE_STRONG_TAG = "</strong>";
  private static final String CAT_OPEN_18_34 = "Open 18-34";
  private static final String MARATHON = "marathon";
  private static final String HALFMARATHON = "halfmarathon";

  private static int openUnder35Position;

  private App() {
  }

  public static void main(final String[] args) throws IOException {
    final int pages;
    final String race;
    if (args[0].equalsIgnoreCase("marathon")) {
      pages = MARATHON_PAGES;
      race = MARATHON;
    } else if (args[0].equalsIgnoreCase("halfmarathon")) {
      pages = HALFMARATHON_PAGES;
      race = HALFMARATHON;
    } else {
      System.out.println(
          "Usage: java -jar bim-ranking-java.jar <race> "
          + "\n <race>: 'maraton' or 'halfmarathon'");
      return;
    }

    for (int i = 1; i < pages; i++) {
      String responseBody = getResponse(i, race);
      List<Result> results = getResults(responseBody);
      for (Result result : results) {
        System.out.println(result);
      }
    }
  }

  private static String getResponse(final int page, final String race)
      throws IOException {
    String responseBody = null;
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
      queryParams.add(new BasicNameValuePair("idEveniment", "2"));
      queryParams.add(new BasicNameValuePair("Result[race]", race));
      queryParams
          .add(new BasicNameValuePair("Result_page", String.valueOf(page)));
      String queryString = URLEncodedUtils.format(queryParams,
          StandardCharsets.UTF_8.name());
      String url = "http://abrc.ro/sportdata/result/ranking";
      HttpGet httpGet = new HttpGet(url + "?" + queryString);

      ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

        @Override
        public String handleResponse(final HttpResponse response)
            throws ClientProtocolException, IOException {
          int status = response.getStatusLine().getStatusCode();
          if (status >= HttpStatus.SC_OK
              && status < HttpStatus.SC_MULTIPLE_CHOICES) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
              return null;
            }
            return EntityUtils.toString(entity);
          } else {
            throw new ClientProtocolException(
                "Unexpected response status: " + status);
          }
        }

      };
      responseBody = httpClient.execute(httpGet, responseHandler);
    }
    return responseBody;
  }

  private static List<Result> getResults(final String responseBody) {
    List<Result> result = new ArrayList<Result>();
    String body = responseBody.substring(
        responseBody.indexOf("<tbody>") + "<tbody>".length(),
        responseBody.indexOf("</tbody>"));
    String[] lines = body.split("\n");
    for (String line : lines) {
      if (line == null || line.isEmpty() || line.startsWith("<tr")) {
        continue;
      }
      Result res = getResult(line);
      result.add(res);
    }
    return result;
  }

  private static Result getResult(final String line) {
    List<String> members = new ArrayList<String>();
    final Matcher m = PATTERN.matcher(line);
    while (!m.hitEnd() && m.find()) {
      String group = m.group();
      String value = getValueFromGroup(group);
      members.add(value);
    }
    final String race = getStringValue(members.get(0));
    final int generalPosition = getIntValue(members.get(1));
    final int bib = getIntValue(members.get(2));
    final String fullName = getStringValue(members.get(3));
    final String nationality = getStringValue(members.get(4));
    final String category = getStringValue(members.get(5));
    final int categoryPosition = getIntValue(members.get(6));
    final String officialTime = getStringValue(members.get(7));
    final String netTime = getStringValue(members.get(8));
    final int catPos;
    final String cat;

    if (category.isEmpty()) {
      cat = CAT_OPEN_18_34;
    } else {
      cat = category;
    }

    if (categoryPosition == NO_CAT_POSITION) {
      catPos = openUnder35Position++;
    } else {
      catPos = categoryPosition;
    }

    Result result = new Result(race, generalPosition, bib, fullName,
        nationality, cat, catPos, officialTime, netTime);
    return result;
  }

  private static String getValueFromGroup(final String group) {
    if (group == null || group.isEmpty()) {
      return "";
    }
    if (group.contains(OPEN_STRONG_TAG) && group.contains(CLOSE_STRONG_TAG)) {
      return group.substring(OPEN_TD_LENGHT + OPEN_STRONG_LENGTH,
          group.length() - (CLOSE_TD_LENGHT + CLOSE_STRONG_LENGTH));
    }
    return group.substring(OPEN_TD_LENGHT, group.length() - CLOSE_TD_LENGHT);
  }

  private static int getIntValue(final String string) {
    if (string == null || string.isEmpty()) {
      return NO_CAT_POSITION;
    }
    return Integer.parseInt(string);
  }

  private static String getStringValue(final String string) {
    if (string == null || string.isEmpty()) {
      return "";
    }
    return string;
  }

}
