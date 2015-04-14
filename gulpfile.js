// Include gulp
var gulp = require('gulp'),
    glob = require('glob'),
    browserify = require('browserify'),
    ngAnnotate = require('browserify-ngannotate'),
    debowerify = require('debowerify'),
    source = require('vinyl-source-stream'),
    buffer = require('vinyl-buffer'),
    uglify = require('gulp-uglify'),
    clean = require('gulp-clean'),
    jshint = require('gulp-jshint'),
    sass = require('gulp-sass'),
    install = require('gulp-install'),
    concat = require('gulp-concat');

/*
 * Path configurations to minimize error down the line
 */
var paths = {
  scripts: {
    src: "./src/main/webapp/js/src/**/*.js",
    entry: "./src/main/webapp/js/src/app.js",
    dest: "src/main/webapp/js/compiled/"
  },
  styles: {
    src: "./src/main/webapp/scss/src/**/*.scss",
    deps: [
      "./node_modules/ng-dialog/css/ngDialog.css",
      "./node_modules/ng-dialog/css/ngDialog-theme-default.css",
      "./node_modules/ng-dialog/css/ngDialog-theme-plain.css"
    ],
    dest: "src/main/webapp/scss/compiled/",
    
    /*
     * Each of the subfolders within the following paths will
     * be available to scss/css @import statements
     */
    includePaths: [
      "./bower_components/foundation/scss",
      "./bower_components/foundation/css"
    ]
  }
};

gulp.task('clean-scripts', function() {
  return gulp.src(paths.scripts.dest, {read: false})
             .pipe(clean());
})

gulp.task('clean-styles', function() {
  return gulp.src(paths.styles.dest, {read: false})
             .pipe(clean());
})        

//Lint Task
gulp.task('lint', function() {
return gulp.src(paths.scripts.src)
   .pipe(jshint())
   .pipe(jshint.reporter('default'));
});

gulp.task('install-bower-deps', function() {
  return gulp.src(['./bower.json'])
             .pipe(install());
})

gulp.task('scripts', ['install-bower-deps', 'clean-scripts'], function() {
  return browserify({ 
    entries: [paths.scripts.entry]
  })
  .transform(debowerify)
  .transform(ngAnnotate)
  .bundle()
  .pipe(source('bundle.js'))
  .pipe(buffer())
  .pipe(uglify())
  .pipe(gulp.dest(paths.scripts.dest));
});

// Move the CSS dependencies into the compile directory
gulp.task('style-deps', ['install-bower-deps'], function() {
  return gulp.src(paths.styles.deps)
             .pipe(gulp.dest(paths.styles.dest));
});

gulp.task('styles', ['install-bower-deps', 'clean-styles', 'style-deps'], function() {
  return gulp.src(paths.styles.src)
             .pipe(sass({ includePaths: paths.styles.includePaths }))
             .pipe(gulp.dest(paths.styles.dest));
});

// Watch Files For Changes
gulp.task('watch', function() {
  gulp.watch(paths.scripts.src, ['lint', 'scripts']);
  gulp.watch(paths.scripts.src, ['styles']);
});

// Default Task
gulp.task('default', ['lint', 'scripts', 'styles']);

gulp.task('develop', ['default', 'watch']);