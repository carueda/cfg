2018-02-12 0.3.0

- just setting version to 0.3.0 as it seems I published a 
  0.2.0 version by mistake.
  
2018-02-11 0.0.8

- fix #1: "Backticks not properly handled".
  However, unfortunately:
  - Used scalameta+paradise versions are not expanding the macro
    correctly. So, the tests below are commented out.
    Interestingly, IDEA expands the cases below just fine, and
    the tests would pass as expected.
    Quick attempts to try newer versions of scalameta+parasise 
    proved tricky. And this is most likely because...
 
  - More in general, scalameta macros are not part of that
    project anymore! This simply means a stable macro support
    for scala continues to be in a limbo.
 

2017-09-01 0.0.7

- no actual change but need to regenerate and publish

2017-08-08 0.0.5

- support `Option[T]`
- expose scala.concurrent.duration.Duration instead of java.time.Duration

2017-08-07

- first pretty usable version
