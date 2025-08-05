# Pension Lifetime Allowance (PLA) Dynamic Stub

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Build Status](https://travis-ci.org/hmrc/pla-dynamic-stub.svg?branch=master)](https://travis-ci.org/hmrc/pla-dynamic-stub) [ ![Download](https://api.bintray.com/packages/hmrc/releases/pla-dynamic-stub/images/download.svg) ](https://bintray.com/hmrc/releases/pla-dynamic-stub/_latestVersion)

This is a stub designed to be used with the [Pensions Lifetime Allowance](https://github.com/hmrc/pensions-lifetime-allowance/) service. The stub is a test double that supports the HOD (Head Of Duty system) API in development or test environments, this enables testing of the service without requiring a full end-to-end test environment that has all the integration and HOD systems available.

The stub is a Play/Scala application backed by a Mongo database for the test data, which is dynamically created (hence it is termed a dynamic stub, because it does not contain hardcoded, static test data). The test data can be set up either by making requests to the relevant apply or amend operations of the API, or directly loaded into the database using e.g. `mongoimport`. 

The stub supports these PLA service API operations:

- `GET /pensions-lifetime-allowance/individual/{nino}/protections` - get all pension lifetime allowance protections granted to the individual with the specified NINO
- `GET /pensions-lifetime-allowance/individual/{nino}/protections/{protectionId}` - get a specific protection by NINO and protection ID
- `POST /pension-lifetime-allowance/individual/{nino}/protection` - apply for a new protection for the individual with the specified NINO
- `PUT /pensions-lifetime-allowance/individual/{nino}/protection/{protectionId}` - amend the specified existing protection
- `GET  /pensions-lifetime-allowance/scheme-administrator/certificate-lookup?pensionSchemeAdministratorCheckReference={psaRef}&lifetimeAllowanceReference={ltaRef}` - verify that the provided LTA Reference is valid for that individual and return protection details

HIP end points supported:

- `POST /paye/lifetime-allowance/person/{nino}/reference/{id}/sequence-number/{sequence}` - amend a protection

The stub attempts to apply the same business rules as the full production service to protection application and amendment requests, which can return various outcomes based partly on whether and what type of protections are already in place for the individual. A notification ID returned with the response identifies the specific outcome.

(The stub also supports some test-only operations for the purposes of making it easy to set up and tear down test data)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

# Running locally requires starting test routes, like this:
sbt "run 9012 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"
