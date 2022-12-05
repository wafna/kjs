#!/usr/bin/bash

>&2 echo "WARNING do not run this if processes are running in IDEA!"
>&2 read -p "Continue [y/N]? " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    exit 1
fi

./gradlew :clean :build --warning-mode all
