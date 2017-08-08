:+1::tada: First off, thank you for considering contributing to `Cfg`! :+1::tada:  

You can contribute in several ways including bug reports, general suggestions 
(better documentation, improved build process, additional features, perhaps 
comparisons with similar wrappers/tools, etc.), and actual development.

When filing an issue, make sure to consider these questions:
1. What versions of Scala and `Cfg` are you using?
1. What did you do?
1. What did you expect to see?
1. What did you see instead?

This project follows the usual [Fork](https://help.github.com/articles/fork-a-repo/) and 
[Pull Request](https://help.github.com/articles/about-pull-requests/) workflow.
Feel free to fork the repository, add your changes and give back by issuing a pull request. 

**Contribution Guidelines**

- All code PRs should come with a meaningful description, inline comments for 
  important things, unit tests, and a green build.
- Non-trivial changes, including bug fixes, should appear in the changelog. 
  Feel free to add your name and link to your github profile.
- New features should be added to the relevant parts of the documentation.
- It's entirely possible your changes won't be merged, or will get ripped out later. 
  This is also the case for my changes, as the author!
- Even a rejected/reverted PR is valuable! 
- Feel free to send proof-of-concept PRs that you don't intend to get merged.

**Some background**

I'm also the author of https://github.com/carueda/tscfg, which initially only 
targeted Java but later on also became able to generate Scala code from 
configuration "specs" defined by using the same Typesafe Config syntax. 
While tscfg is a code generator to be run at some point prior to the actual
compile phase of the build process,
`Cfg` is a Scalameta macro that can be integrated in the process in a 
much more straightforward fashion.
More importantly, besides boilerplate-free, type-safe _access_ to 
configuration properties, a key goal of `Cfg` is to allow the use
of regular Scala (case classes and nested vals and objects) to 
strictly _specify_ the schema of the configuration.
`Cfg` is my first Scalameta project. Your feedback would be specially 
welcome if you have any suggestions for improvements in this sense. 
