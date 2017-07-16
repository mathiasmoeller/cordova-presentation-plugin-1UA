var gulp = require("gulp");
var uglify = require("gulp-uglify");
var concat = require("gulp-concat");
var replace = require('gulp-replace-task');
var fs = require('fs');
var rename = require("gulp-rename");
var runSequence = require("run-sequence");

gulp.task("uglify", function() {
  return gulp.src(['www/*.js', '!www/NavigatorPresentation.js'])
    .pipe(concat('scripts.js'))
    .pipe(uglify())
    .pipe(gulp.dest('www/dist'));
});

gulp.task("receiver_min", function() {
  return gulp.src(['src/android/receiver.js'])
    .pipe(concat('receiver_min.js'))
    .pipe(uglify())
    .pipe(replace({
      patterns: [
        {
          match: /"/g,
          replacement: function() {
            return "'";
          }
        }
      ]
    }))
    .pipe(gulp.dest('src/android'));
});

var fileContent;

gulp.task("receiver_load", function(callback) {
  fs.readFile('src/android/receiver_min.js', 'utf8', function(err, file) {
    fileContent = file;
    callback();
  })
});

gulp.task('receiver_insert', function () {
  return  gulp.src('src/android/NavigatorPresentationJS_unprepared.java')
    .pipe(replace({
      patterns: [
        {
          match: 'RECEIVER_MIN',
          replacement: fileContent
        }
      ]
    }))
    .pipe(rename("NavigatorPresentationJS.java"))
    .pipe(gulp.dest('src/android/'));
});

gulp.task("default", function(callback) {
  runSequence("uglify", "receiver", callback);
});
gulp.task("receiver", function(callback) {
  runSequence("receiver_min", "receiver_load", "receiver_insert", callback);
});
