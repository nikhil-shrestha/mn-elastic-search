package com.illusionist.es;

import com.illusionist.Constants;
import com.illusionist.TestDataProvider;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Controller("/data")
public class DataEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(TestDataProvider.class);
  private final RestHighLevelClient client;

  public DataEndpoint(RestHighLevelClient client) {
    this.client = client;
  }

  @ExecuteOn(TaskExecutors.IO)
  @Get("/document/{id}")
  public String byId(@PathVariable("id") String documentId) throws IOException {
    var response = client.get(new GetRequest(Constants.INDEX, documentId), RequestOptions.DEFAULT);
    LOG.debug("Response /document/{} => {}", documentId, response.getSourceAsString());
    return response.getSourceAsString();
  }

  @Get("/document/{id}/async")
  public CompletableFuture<String> byIdAsync(@PathVariable("id") String documentId)  {
    final CompletableFuture<String> whenDone = new CompletableFuture<>();
    var response = client.getAsync(new GetRequest(Constants.INDEX, documentId), RequestOptions.DEFAULT, new ActionListener<GetResponse>() {
      @Override
      public void onResponse(GetResponse response) {
        LOG.debug("Response /document/{}/async => {}", documentId, response.getSourceAsString());
        whenDone.complete(response.getSourceAsString());
      }

      @Override
      public void onFailure(Exception e) {
        whenDone.completeExceptionally(e);
      }
    });

    return whenDone;
  }

}
