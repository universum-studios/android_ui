#!/bin/bash

set -ex

# Setup script constants.
TEMP_DIR=website-temp
LIBRARY_NAME=ui
LIBRARY_ARTIFACT_NAME="${LIBRARY_NAME//_/-}"
LIBRARY_VERSION=0.9.1
LIBRARY_REPO="git@github.com:universum-studios/android_${LIBRARY_NAME}.git"
LIBRARY_DIR_ARTIFACTS=../artifacts/universum/studios/android/$LIBRARY_ARTIFACT_NAME/$LIBRARY_VERSION/
LIBRARY_JAVADOC_FILE_NAME="${LIBRARY_ARTIFACT_NAME}-${LIBRARY_VERSION}-javadoc.jar"
LIBRARY_DIR_TESTS=../library/build/reports/androidTests/connected/
LIBRARY_DIR_COVERAGE=../library/build/reports/coverage/debug/
WEBSITE_FILES_VERSION="${LIBRARY_VERSION:0:1}".x
WEBSITE_DIR_DOC=doc/
WEBSITE_DIR_DOC_VERSIONED=$WEBSITE_DIR_DOC$WEBSITE_FILES_VERSION/
WEBSITE_DIR_TESTS=tests/
WEBSITE_DIR_TESTS_VERSIONED=$WEBSITE_DIR_TESTS$WEBSITE_FILES_VERSION/
WEBSITE_DIR_COVERAGE=coverage/
WEBSITE_DIR_COVERAGE_VERSIONED=$WEBSITE_DIR_COVERAGE$WEBSITE_FILES_VERSION/

# Delete left-over temporary directory (if exists).
rm -rf $TEMP_DIR

#  Clone the current repo into temporary directory.
git clone --depth 1 --branch gh-pages $LIBRARY_REPO $TEMP_DIR

# Move working directory into temporary directory.
cd $TEMP_DIR

# Delete all files for the current version.
rm -rf $WEBSITE_DIR_DOC_VERSIONED
rm -rf $WEBSITE_DIR_TESTS_VERSIONED
rm -rf $WEBSITE_DIR_COVERAGE_VERSIONED

# Copy files for documentation and reports for Android tests and Coverage from the primary library module.
# Documentation:
mkdir -p $WEBSITE_DIR_DOC_VERSIONED
cp $LIBRARY_DIR_ARTIFACTS$LIBRARY_JAVADOC_FILE_NAME $WEBSITE_DIR_DOC_VERSIONED$LIBRARY_JAVADOC_FILE_NAME
unzip $WEBSITE_DIR_DOC_VERSIONED$LIBRARY_JAVADOC_FILE_NAME -d $WEBSITE_DIR_DOC_VERSIONED
rm $WEBSITE_DIR_DOC_VERSIONED$LIBRARY_JAVADOC_FILE_NAME
# Tests reports:
mkdir -p $WEBSITE_DIR_TESTS_VERSIONED
cp -R $LIBRARY_DIR_TESTS. $WEBSITE_DIR_TESTS_VERSIONED
# Coverage reports:
mkdir -p $WEBSITE_DIR_COVERAGE_VERSIONED
cp -R $LIBRARY_DIR_COVERAGE. $WEBSITE_DIR_COVERAGE_VERSIONED

# Commit and push only if some of the website files have changed.
if ! git diff-index --quiet HEAD --; then
    # Stage all files in git and create a commit.
    git add .
    git add -u
    git commit -m "Website at $(date)."

    # Push the new website files up to the GitHub.
    git push origin gh-pages
fi

# Delete temporary directory.
cd ..
rm -rf $TEMP_DIR