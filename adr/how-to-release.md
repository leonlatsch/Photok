# How to release this app

> In case I forget it again :)


## 1. Create Release Candidate

To start a release run the "Create Release Candidate" Action in GitHub Actions.
This will create a release/x.x.x branch.

On this branch it will update the Android Version Code & Name and update the oss report.

Afterwords it will create a PR into master like: "Release x.x.x".
Review this PR before stepping forward.

## 2. Merge in master

Once you reviewed the release PR you can approve and merge it into master.

## 3. Build Release Artifacts

After merging into master you can run the "Build and Upload Signed Release" GitHub Action.

This will, on master, build a standalone and Google Play version of Photok and sign it.
These Artifacts will be uploaded to GitHub and to leonlatsch's private cloud.

## 4. Create GitHub & GPlay Release

### Preparations

**GitHub**

Once you have the Artifacts, you can draft a new GitHub Release.
For Patch Notes, just use the auto generated ones and remove the release pr's.

Upload the standalone version to the GitHub release and save the draft.

**Google Play**

For Google Play, create a new release in the Play Console.
Upload the gplay (aab) version of Photok there.

For Patch Notes, summarize the auto generated ones in nice english/german.

### Publishing

Once both releases are prepared, publish both at the same time.
F-Droid will be automatically released with the GitHub tag being created. 

## 5. Finish Release

Once Google Play has accepted the Release, create a PR to merge `master` back into `develop` and merge it.

**Congrats** the release is finished!