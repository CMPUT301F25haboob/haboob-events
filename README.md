# Navigation

To visit the CRC, go to Wiki, there exists a page called CRC containing all the most improtant and anticipated classes their responsibilities and collaborators. 

To visit the product BackLog, go to Projects which contains project board "Product Backlog"

To visit the Storyboard Sequences, go to Wiki, there exists a page there called Storyboard which contains the Storyboards.

# Git Workflow Instructions

## Preamble

If you don't have a lot of experience with git (or even if you do), do yourself a favour and download GitHub Desktop here: https://desktop.github.com/download/

It makes using git way easier, especially when you are contantly branching and making pull requests. If you need a tutorial on it let me(Dan) know.

You can also use the source control extension in VS Code for easy commits/pushes. Or just use terminal if you're a freak.

## General Flow

We will have a branch off main called **dev**. We will ONLY directly branch of of dev to develop feature. I will call these branches off dev **feature branches**. Once a feature is complete, you will test it and then create a pull request to merge your branch with **dev**. Once we hit specific milestones, we can test the dev branch thoroughly and then merge it with main. Main should always contain a working version of our app with no KNOWN bugs (bugs will always exist but you shouldn't knowing push buggy code).

See below for detailed instructions on the workflow

## Setp-by-Step Instructions

1. Make sure you are on the **dev** branch. If you are using terminal, runt his command: `git checkout dev`
   Then run `git status` and make sure it says you are on the branch dev.
   If you are on github desktop, simply select the dev branch as your current branch
   <img width="1251" height="722" alt="Screenshot 2025-10-22 at 9 31 27 PM" src="https://github.com/user-attachments/assets/cdbec7ec-2473-47b4-b3ce-dc1d3a6db823" />

