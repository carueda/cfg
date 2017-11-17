:+1: First off, thank you for considering contributing to `Cfg`! :+1:  

You can contribute in several ways including bug reports, general suggestions 
(better documentation, improved build process, additional features, perhaps 
comparisons with similar wrappers/tools, etc.), and actual development.

This project follows the usual [Fork](https://help.github.com/articles/fork-a-repo/) and 
[Pull Request](https://help.github.com/articles/about-pull-requests/) workflow.
Feel free to fork the repository, add your changes and give back by issuing a pull request. 

All code PRs should come with a meaningful description, inline comments for 
important things, unit tests, and a green build.

**Some background**

I'm also the author of https://github.com/carueda/tscfg, which initially 
targeted Java but later on also became able to generate Scala code from 
configuration "specs" defined by using the same Typesafe Config syntax. 
While tscfg is a code generator to be run at some point prior to the actual
compile phase of the build process,
`Cfg` is a Scalameta macro that can be integrated in the process in a 
much more straightforward fashion.
More importantly, besides boilerplate-free, type-safe _**access**_ to 
configuration properties, a key goal of `Cfg` is to allow the use
of regular Scala (case classes and nested vals and objects) to 
strictly _**specify**_ the schema of the configuration.
`Cfg` is my first Scalameta project. Your feedback would be specially 
welcome if you have any suggestions for improvements in this sense. 
