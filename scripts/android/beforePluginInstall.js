#!/usr/bin/env node

'use strict';

const fs = require('fs');
const _ = require('lodash');

module.exports = function(context) {
  const stylesPath = context.opts.projectRoot + '/platforms/android/res/values/styles.xml';
  const colorPath = context.opts.projectRoot + '/platforms/android/res/values/color.xml';

  if (!fs.existsSync(stylesPath)) {
    fs.writeFile(stylesPath, "<resources></resources>", function(err) {
      if (err) {
        return console.log(err);
      }

      console.log("New MainActvity generated.");
    });
  }

  if (!fs.existsSync(colorPath)) {
    fs.writeFile(colorPath, "<resources></resources>", function(err) {
      if (err) {
        return console.log(err);
      }

      console.log("New MainActvity generated.");
    });
  }
};
