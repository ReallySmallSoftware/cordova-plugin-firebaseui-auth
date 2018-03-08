#!/usr/bin/env node

'use strict';

const fs = require('fs');

module.exports = {

  getAndroidResPath: function(context) {

    var platforms = context.opts.cordova.platforms;

    if (platforms.indexOf("android") === -1) {
      return null;
    }

    var androidPath = context.opts.projectRoot + '/platforms/android';

    if (!fs.existsSync(androidPath)) {
      androidPath = context.opts.projectRoot + '/platforms/android/app/src/main';

      if (!fs.existsSync(androidPath)) {
        console.log("Unable to detect type of cordova-android application structure");
        throw new Error("Unable to detect type of cordova-android application structure");
      } else {
        console.log("Detected cordova-android 7 application structure");
      }
    } else {
      console.log("Detected pre cordova-android 7 application structure");
    }

    return androidPath;
  },

  getAndroidManifestPath: function(context) {
    return this.getAndroidResPath(context);
  },

  getAndroidSourcePath: function(context) {
    var platforms = context.opts.cordova.platforms;

    if (platforms.indexOf("android") === -1) {
      return null;
    }

    var androidPath = context.opts.projectRoot + '/platforms/android/src';

    if (!fs.existsSync(androidPath)) {
      androidPath = context.opts.projectRoot + '/platforms/android/app/src/main/java';

      if (!fs.existsSync(androidPath)) {
        console.log("Unable to detect type of cordova-android application structure");
        throw new Error("Unable to detect type of cordova-android application structure");
      } else {
        console.log("Detected cordova-android 7 application structure");
      }
    } else {
      console.log("Detected pre cordova-android 7 application structure");
    }

    return androidPath;
  }
};
