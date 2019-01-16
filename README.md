# billpay-test-server [![CircleCI](https://circleci.com/gh/electrumpayments/billpay-test-server/tree/master.svg?style=shield)](https://circleci.com/gh/electrumpayments/billpay-test-server/tree/master)
REST server for testing implementations of the [billpay-service-interface](https://github.com/electrumpayments/billpay-service-interface).

## Testing a Client
To test a client implementation an instance of this test server is provided at: https://billpay-test-server.herokuapp.com.

Or you can run a containerized version of this test server locally using Docker:
```bash
docker pull electrum/billpay-test-server:4
# Run the test server listening on localhost:8080
docker run -d -p 8080:8080 electrum/billpay-test-server:4
```

Messages sent to this server via the URLs described in the [billpay-service-interface](https://github.com/electrumpayments/billpay-service-interface) will be
validated as well as processed against a set of preloaded mock customer accounts.

### Test utils
| Action                                                  | Url                     |
|---------------------------------------------------------|-------------------------|
| View all loaded customer accounts and their information | /test/allAccounts       |
| View all AccountLookupRequests that have been made     | /test/allAccountLookups |
| View all PaymentRequests that have been made     | /test/allPaymentRequests |
| View all RefundRequests that have been made     | /test/allRefundRequests |
| View all PaymentConfirmations that have been made     | /test/allPaymentConfirmations |
| View all PaymentReversals that have been made     | /test/allPaymentReversals |
| View all RefundConfirmations that have been made     | /test/allRefundConfirmations |
| View all RefundReversals that have been made     | /test/allRefundReversals |
| Reset all accounts and remove all messges received     | /test/reset |
| Add a BillPayment account     | /test/addAccount |

### Testing message correctness
Messages will be validated for correctness against the service interface, in the event that a field is missing something similar to the following can be expected:

```json
{
  "errorType": "FORMAT_ERROR",
  "errorMessage": "See error detail for format errors.",
  "detailMessage": [
    {
      "messageProperty": "message",
      "field": "time",
      "error": "may not be null"
    },
    {
      "messageProperty": "institution",
      "field": "id",
      "error": "must match \"[0-9]{1,11}\"",
      "invalidValue": "hjg77"
    },
    {
      "messageProperty": "merchant",
      "field": "merchantId",
      "error": "may not be null"
    }
  ]
}
```

An errorType of `FORMAT_ERROR` is returned followed by an explanation of the format errors as follows:

* The `messageProperty` attribute containing the element in which the error occurs
* The `field` attribute containing the field that has been formatted incorrectly
* The `error` field contains information on what violation has occurred
* The `invalidValue` field contains the incorrectly formatted value that was used

### Customer Accounts
Forty-five mock customer accounts are loaded and are available for the testing of payment flows. For a example changes to an accounts balance via a PaymentRequest and PaymentConfirmation
will remain unless a RefundRequest and RefundConfirmation are made for said PaymentRequest. Details about all test accounts can be seen using `/test/allAccounts`.

New BillPayment accounts can be added via `/test/addAccount` with JSON similar to the following:

```json
{
    "accountRef": "3523234233",
    "balance": {
        "amount": 54222,
        "currency": "ZAR"
    },
    "customer": {
        "address": "Gentle Bay Hornets Nest, Midgard",
        "contactNumber": "4410424778",
        "firstName": "Colbey",
        "idNumber": "3967397865159",
        "lastName": "Calistinsson"
    }
}
```

The only mandatory field here is `accountRef` neglecting to poplulate the other fields will result in server generated data being used.
For example the following request JSON is valid:

```json
{
    "accountRef": "3523234233"
}
```

### Message State
Validation is also performed on the different messages as they relate to other messages that have been received (or not received).
For example if a message is received with an ID that has already been used in a previous message something similar to the following can be expected:

```json
{
  "errorType": "DUPLICATE_RECORD",
  "errorMessage": "Message ID (UUID) is not unique.",
  "detailMessage": "506bdc64-256d-ae37-8b27-6773c78c7345"
}
```

Or, if a reversal request is sent after a confirmation (or vice-versa) something similar to the following can be expected:

```json
{
  "errorType": "ACCOUNT_ALREADY_SETTLED",
  "errorMessage": "Preceding advice  (ID: 02bf36cc-65c4-c189-db60-92fb033e8368) for request found. Use GET /test/allPaymentConfirmations or /test/allPaymentReversals or /test/allRefundConfirmations or /test/allRefundReversals to see all advices",
  "detailMessage": {
    "id": "02bf36cc-65c4-c189-db60-92fb033e8368",
    "requestId": "ed7d3ed0-4a76-3161-78f2-f404ba5d2b1f",
    "time": "2016-08-16T23:13:39.091Z",
    "linkData": "undefined",
    "tenders": [
      {
        "accountType": "DEFAULT",
        "amount": {
          "amount": 8160,
          "currency": "ZAR"
        },
        "cardNumber": "12345647652312341",
        "reference": "1",
        "tenderType": "CASH"
      },
      {
        "accountType": "DEFAULT",
        "amount": {
          "amount": 8160,
          "currency": "ZAR"
        },
        "cardNumber": "12345647652312342",
        "reference": "2",
        "tenderType": "CREDIT_CARD"
      }
    ]
  }
}
```

Or, if a confirmation or reversal are sent but no request precedes them, something similar to the following can be expected:

```json
{
  "errorType": "UNABLE_TO_LOCATE_RECORD",
  "errorMessage": "No preceding request (ID: 05ba6f76-106d-6138-cd6c-a685e18ccbd6) found for advice. Use GET /test/allPaymentRequests or /test/allRefundRequests to see all requests"
}
```

## Testing a Server
Testing a server implementation can be achieved using [this](https://github.com/electrumpayments/billpay-test-server/tree/master/test/postman) Postman (Collection v2) REST test pack.
These tests consist of correctly formatted JSON messages that validate server responses. Tests may also consist of a message flow in which multiple related messages are sent sequentially to the server to test handling of state-full interactions (such as requests and confirmations).

The test pack is comprised of three JSON files: `billpaytest_server_tests.postman_collection.json` , `heroku.postman_environment.json` and `localhost.postman_environment.json`.
The first file is a collection of JSON tests that will be run, herein one will find JSON request messages and response validation scripts. These tests are dependant on variables contained in the the preceding two files, these being identical save for the server endpoint they point to:

```json
{
  "enabled": true,
  "key": "url",
  "type": "text",
  "value": "https://billpay-test-server.herokuapp.com"
}
```

Changing the above property within an environment will change the endpoint to which messages are sent.

### Running tests

There are two possible ways to run this test pack: either via the Postman desktop client or via Newman, the command line interface for Postman.

#### Postman
1. Download Postman at: https://www.getpostman.com/apps
2. Import the test collection and environments via the Import option
3. Open the Collection Runner and select the Runs tab
4. Select a test collection and environment and hit Start Test. Note that individual test subsections may be selected.

Note that that tests may be run individually from the main Postman view where test conditions and structures may be modified.

#### Newman
1. Install newman (make sure `npm` is installed first):
```
	npm install newman -g
```
2. Run the tests (from the root directory of this reop):
```
	newman run test/postman/billpaytest_server_tests.postman_collection.json -e test/postman/localhost.postman_environment.json
```
This will run all tests and provide a basic breakdown of which tests passed and failed.
