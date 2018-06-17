#!/usr/bin/env bash

set -eu

mvn test -B

tests="$(grep '<testsuite' target/surefire-reports/TEST-* | grep -Eo 'tests="[0-9]+"' | grep -Eo "[0-9]+" | paste -sd+ - | bc)"
skipped="$(grep '<testsuite' target/surefire-reports/TEST-* | grep -Eo 'skipped="[0-9]+"' | grep -Eo "[0-9]+" | paste -sd+ - | bc)"

percentage_complete="$(bc <<< "($tests - $skipped) * 100 / $tests")"

sed -E -i '' "s/[0-9]+% Done/$percentage_complete% Done/" README.md
sed -E -i '' "s|http://progressed.io/bar/[0-9]+|http://progressed.io/bar/$percentage_complete|" README.md

