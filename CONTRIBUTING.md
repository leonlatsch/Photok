# Contributing to Photok

Everybody is free to contribute to Photok.
However, there are a few guidelines you should respect.

## General

1. If you want to contribute, please ensure, that I am able to contact you in any way.
3. USE the PR template.
4. If you haven't contributed already, you may add an entry in `contributors.json` in the android assets, to be added to the "Credits" section in the app.
5. An of course, please read the code of conduct and stick to it.

## Translations

If you want to help translate Photok you are very welcome to do so.

**Improve an existing language**

There are several `strings.xml`  files in `app/src/main/res/values[your_language]/strings.xml`.
In these files un-translated texts are marked with `<!-- TODO -->`. Translate these and remove the marker.
Open a Pull Request with your new text.

**Add a new language**

Adding a new language is about the same process as editing an existing one.

You will need to create a new file in `app/src/main/res/values[your_language]/strings.xml`
*Copy* the contents of the english `strings.xml` and translate them.
Open a Pull Request with your new text.

The folder name of Android string files is formatted as the following:

- without region variant: values-[locale]
- with region variant: values-[locale]-r[region]
- For example: values-en, values-en-rGB, values-el-rGR.

## New Features

New features should be implemented respecting the following guidelines:
- Use Jetpack Compose for UI (fragments for navigation for now)
- Use [Clean Architecture](https://cdn-media-1.freecodecamp.org/images/oVVbTLR5gXHgP8Ehlz1qzRm5LLjX9kv2Zri6) as a Guideline (data/domain/ui)
- Orient yourself on existing features for code style, etc.
