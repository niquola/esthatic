#!/bin/bash
# set -e # exit with nonzero exit code if anything fails

# clear and re-create the out directory
rm -rf dist || exit 0;
mkdir dist;

# run our compile script, discussed above
#env SITE_URL='/new-site/' lein generate
lein generate

# go to the out directory and create a *new* Git repo
cd dist
rm .git -rf
git init

# inside this git repo we'll pretend to be a new user
git config user.name "Travis CI"
git config user.email "niquola@gmail.com"

# The first and only commit to this new Git repo contains all the
# files present with the commit message "Deploy to GitHub Pages".
git checkout -b gh-pages
git add .
git commit -m "Deploy to GitHub Pages"

# Force push from the current repo's master branch to the remote
# repo's gh-pages branch. (All previous history on the gh-pages branch
# will be lost, since we are overwriting it.) We redirect any output to
# /dev/null to hide any sensitive credential data that might otherwise be exposed.

eval $(ssh-agent)
ls -lah .
chmod 400 ../secure/key
ssh-add ../secure/key

echo 'Add origin'
git remote add origin git@github.com:HealthSamurai/new-site.git
ssh-keyscan -H github.com > ~/.ssh/known_hosts
echo 'Push to origin'
git config --global push.default simple
git push -f origin gh-pages
echo 'Done'
