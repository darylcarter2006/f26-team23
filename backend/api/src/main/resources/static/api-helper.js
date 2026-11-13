// Sends a request to the API, returns the parsed JSON, and throws an Error with the server's message on failure.
async function sendApiRequest(requestPath, requestOptions) {
  let apiResponse;
  try {
    apiResponse = await fetch(requestPath, requestOptions);
  } catch (networkError) {
    throw new Error("Cannot reach the server. Is the application running?");
  }

  const responseBody = await parseJsonBodyIfPresent(apiResponse);
  if (!apiResponse.ok) {
    const serverErrorMessage = responseBody && responseBody.message;
    throw new Error(serverErrorMessage || "Request failed with status " + apiResponse.status);
  }
  return responseBody;
}

// Reads the response body as JSON, or returns null when the body is empty or not JSON.
async function parseJsonBodyIfPresent(apiResponse) {
  const responseText = await apiResponse.text();
  if (!responseText) {
    return null;
  }
  try {
    return JSON.parse(responseText);
  } catch (parseError) {
    return null;
  }
}
