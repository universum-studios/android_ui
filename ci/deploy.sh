#!/usr/bin/env bash

if [[ ${TRAVIS_BRANCH} =~ .*master.* ]]; then
  ./gradlew deployToBintray deployToGitHub
fi