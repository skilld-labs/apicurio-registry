#!/bin/bash

declare -a remotes=("upstream" "origin")
REMOTE=""
for r in "${remotes[@]}"
do
  if git remote | grep "$r"; then
    read -p "Using $r remote. Is that OK? [yes] " CONTINUE
    if [ "x$CONTINUE" = "xyes" ] || [ -z "$CONTINUE" ]; then
      REMOTE="$r"
      break
    fi
  fi
done

if [ -z "$REMOTE" ]; then
  echo "Quitting."
  exit 1
fi

echo "Merging latest changes from the main branch!"
git fetch $REMOTE main
git merge $REMOTE/main

echo "Merge complete.  Check for conflicts!"
echo ""
while [ "x$CONTINUE" != "xyes" ]
do
  read -p "Have you fixed all conflicts (and commited them)? [yes]" CONTINUE
  if [[ -z $CONTINUE ]] ; then
    CONTINUE=yes
  fi
done

echo "OK great, updating MAS build number."
MAS_BUILD_NUMBER=`mvn help:evaluate -Dexpression=mas.build.number -q -DforceStdout`
NEW_MAS_BUILD_NUMBER=$(($MAS_BUILD_NUMBER+1))

echo "Updating the MAS build number to: $NEW_MAS_BUILD_NUMBER"
mvn versions:set-property -Dproperty=mas.build.number -DgenerateBackupPoms=false -DnewVersion=$NEW_MAS_BUILD_NUMBER

git status
echo "---"
echo "MAS build number updated (see changes above)."
echo "MAS build number is now set to: $NEW_MAS_BUILD_NUMBER"
echo "---"

CONTINUE="no"
while [ "x$CONTINUE" != "xyes" ]
do
  read -p "OK to push changes to 'mas-sr' branch? [yes]" CONTINUE
  if [[ -z $CONTINUE ]] ; then
    CONTINUE=yes
  fi
done

git add .
git commit -m "Updated MAS build number to $NEW_MAS_BUILD_NUMBER"
git push $REMOTE mas-sr

echo "All done!  Everything was successful.  Great job.  You're killing it!"
