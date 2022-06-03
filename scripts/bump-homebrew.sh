#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

URL="https://api.github.com/repos/spotify/gcs-tools/releases/latest"
TEMP=$(mktemp -d)
echo "Using temporary directory $TEMP"

curl -s $URL > $TEMP/latest
VERSION=$(jq -r '.tag_name' $TEMP/latest | sed "s/^v//g" )
echo "Latest gcs-tools release: $VERSION"

JARS=$(jq -r '.assets[].browser_download_url' $TEMP/latest)
echo "$JARS"

cd $TEMP
echo "Cloning spotify/homebrew-public"
git clone -q --depth 10 git@github.com:spotify/homebrew-public.git
cd homebrew-public

for TOOL in avro-tools magnolify-tools parquet-cli proto-tools; do
    echo "Updating gcs-$TOOL"
    URL=$(echo "$JARS" | grep $TOOL)
    SHASUM=$(curl -sL $URL | shasum -a 256 | awk '{print $1}')
    JAR=$(basename $URL)
    echo $SHASUM $JAR
    cat gcs-$TOOL.rb | \
        sed "s/url \"[^\"]*\"/url \"${URL//\//\\/}\"/g" | \
        sed "s/sha256 \"[^\"]*\"/sha256 \"$SHASUM\"/g" | \
        sed "s/version \"[^\"]*\"/version \"$VERSION\"/g" | \
        sed "s/libexec\.install \"[^\"]*\"/libexec.install \"$JAR\"/g" | \
        sed "s/libexec\/\"[^\"]*\", \"$TOOL\"/libexec\/\"$JAR\", \"$TOOL\"/g" \
        > gcs-$TOOL.rb.tmp
    mv gcs-$TOOL.rb.tmp gcs-$TOOL.rb
done

git update-index -q --refresh
STATUS=0
git diff-index --quiet HEAD -- || STATUS=$?
if [ $STATUS -ne 0 ]; then
    echo "Submitting PR"
    git commit -a -m "Bump gcs-tools to $VERSION"
    BRANCH="$(whoami)/gcs-tools-$VERSION"
    git push -u origin "HEAD:$BRANCH"
    hub pull-request -f -m "Bump gcs-tools to $VERSION" -h $BRANCH
else
    echo "Failed to update repo"
fi

rm -rf $TEMP
