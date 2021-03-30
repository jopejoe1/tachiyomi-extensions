
#!/bin/bash
set -e

TOOLS="$(ls -d ${ANDROID_HOME}/build-tools/* | tail -1)"

shopt -s globstar nullglob extglob
APKS=( **/*".apk" )

# Fail if too little extensions seem to have been built
if [ "${#APKS[@]}" -le "1" ]; then
    echo "Insufficient amount of APKs found. Please check the project configuration."
    exit 1;
fi;

# Take base64 encoded key input and put it into a file

DEST=$PWD/apk
rm -rf $DEST && mkdir -p $DEST

MAX_PARALLEL=4

# Sign all of the APKs
for APK in ${APKS[@]}; do
    (
        BASENAME=$(basename $APK)
        APKNAME="${BASENAME%%+(-release*)}.apk"
        APKDEST="$DEST/$APKNAME"

        ${TOOLS}/zipalign -c -v -p 4 $APK

        cp $APK $APKDEST
    ) &

    # Allow to execute up to $MAX_PARALLEL jobs in parallel
    if [[ $(jobs -r -p | wc -l) -ge $MAX_PARALLEL ]]; then
        wait -n
    fi
done

wait
