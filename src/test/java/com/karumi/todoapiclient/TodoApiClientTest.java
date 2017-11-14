/*
 *   Copyright (C) 2016 Karumi.
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.karumi.todoapiclient;

import com.karumi.todoapiclient.dto.TaskDto;
import java.util.List;

import com.karumi.todoapiclient.exception.ItemNotFoundException;
import com.karumi.todoapiclient.exception.UnknownErrorException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class TodoApiClientTest extends MockWebServerTest {

  private static final String ANY_ID = "any";
  private static final TaskDto ANY_TASK = mock(TaskDto.class);
  private TodoApiClient apiClient;

  @Before public void setUp() throws Exception {
    super.setUp();
    String mockWebServerEndpoint = getBaseEndpoint();
    apiClient = new TodoApiClient(mockWebServerEndpoint);
  }

  @Test public void sendsAcceptAndContentTypeHeaders() throws Exception {
    enqueueMockResponse();

    apiClient.getAllTasks();

    assertRequestContainsHeader("Accept", "application/json");
  }

  @Test public void sendsGetAllTaskRequestToTheCorrectEndpoint() throws Exception {
    enqueueMockResponse();

    apiClient.getAllTasks();

    assertGetRequestSentTo("/todos");
  }

  @Test public void parsesTasksProperlyGettingAllTheTasks() throws Exception {
    enqueueMockResponse(200, "getTasksResponse.json");

    List<TaskDto> tasks = apiClient.getAllTasks();

    assertEquals(tasks.size(), 200);
    assertTaskContainsExpectedValues(tasks.get(0));
  }

  @Test(expected = UnknownErrorException.class) 
  public void shouldReturnAnExceptionWhenGetAllReceivedAnServerError() throws Exception{
    enqueueMockResponse(414);
    apiClient.getAllTasks();
  }
  
  @Test(expected = ItemNotFoundException.class)
  public void shouldReturnItemNotFoundExceptionWhenReceive404errorCode() throws Exception{
    enqueueMockResponse(404);

    apiClient.getTaskById(ANY_ID);
  }

  @Test public void shouldReturnAValidTaskForGivenId() throws Exception{
    enqueueMockResponse(200,"getTaskByIdResponse.json");

    TaskDto task = apiClient.getTaskById("1");

    assertTaskContainsExpectedValues(task);

  }

  @Test public void shouldSendAValidIdInTheUrl() throws Exception{
    enqueueMockResponse();

    apiClient.getTaskById(ANY_ID);

    assertGetRequestSentTo("/todos/" + ANY_ID);

  }

  @Test public void postNewTodoResponseIsCorrect() throws Exception{
    enqueueMockResponse(200,"addTaskResponse.json");

    TaskDto task = apiClient.addTask(ANY_TASK);

    assertTaskContainsExpectedValues(task);

  }

  @Test public void postNewTodoBodyAndIsCorrect() throws Exception{
    enqueueMockResponse(200,"addTaskResponse.json");
    TaskDto newTask = new TaskDto("1","2","Finish this kata",false);

    TaskDto taskDto = apiClient.addTask(newTask);

    assertTaskContainsExpectedValues(taskDto);
    assertRequestBodyEquals("addTaskRequest.json");
  }

  @Test public void posNewTodoIsSentToValidUrl() throws Exception{
    enqueueMockResponse();

    apiClient.addTask(ANY_TASK);

    assertPostRequestSentTo("/todos");

  }



  
  private void assertTaskContainsExpectedValues(TaskDto task) {
    assertEquals(task.getId(), "1");
    assertEquals(task.getUserId(), "1");
    assertEquals(task.getTitle(), "delectus aut autem");
    assertFalse(task.isFinished());
  }
}
