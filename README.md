# billpay-test-server
REST server for testing a client implementation of the [billpay-service-interface](https://github.com/electrumpayments/billpay-service-interface) against.

##Testing a Client
To test a client implementation an instance of this test server is provided at: https://billpay-test-server.herokuapp.com. Messages sent to this server via the urls described in the [billpay-service-interface](https://github.com/electrumpayments/billpay-service-interface) will be validated as well as processed against a set of preloaded mock customer accounts.

####Test utils
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


###Testing message correctness
Messages will be validated for correctness against the service interface, in the event that a field is missing something familiar to the following can be expected:

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

* The "messageProperty" attribute containing the element in which the error occurs
* The "field"  attribute containing the field that has been formatted incorrectly
* The "error" field contains information on what violation has occured
* The "invalidValue" field contains the incorrectly formatted value that was used

###Customer Accounts
Forty-five mock customer accounts are loaded and are available for the testing of payment flows. For a example changes to an accounts balance via a PaymentRequest and PaymentConfirmation will remain unless a RefundRequest and RefundConfirmation are made for said PaymentRequest. Details about all test accounts can be seen using `/test/allAccounts`.

###Message State
Validation is also performed on the different messages as they relate to other messages that have been received or not received. For example if a message is with a ID that has already been used in a previous message something similar to the following will occur:

```json
{
  "errorType": "DUPLICATE_RECORD",
  "errorMessage": "Message ID (UUID) is not unique.",
  "detailMessage": "506bdc64-256d-ae37-8b27-6773c78c7345"
}
```

Or, if a reversal request is sent after a confirmation (or vice-versa) something similar to the following will occur:

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

Or, if a confirmation or reversal is sent but no request precedes them, something similar to the following will occur:

```json
{
  "errorType": "UNABLE_TO_LOCATE_RECORD",
  "errorMessage": "No preceding request (ID: 05ba6f76-106d-6138-cd6c-a685e18ccbd6) found for advice. Use GET /test/allPaymentRequests or /test/allRefundRequests to see all requests"
}
```
