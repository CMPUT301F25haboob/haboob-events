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

### 1. Make sure you are on the **dev** branch
If you are using terminal, runt this command: `git checkout dev`
Then run `git status` and make sure it says you are on the branch dev.
If you are on github desktop, simply select the dev branch as your current branch
<img width="1251" height="722" alt="Screenshot 2025-10-22 at 9 31 27 PM" src="https://github.com/user-attachments/assets/cdbec7ec-2473-47b4-b3ce-dc1d3a6db823" />

### 2. Create your feature branch off dev
First I will mention branch naming. Your branches should follow this naming conventions: `name-userstory`
For example, if I was working on user story US 01.01.04, my branch name would be: `dan-US-01.01.04`
NOTE: I replaced the space after "US" with a dash. Git will automatically do this for you (I think), but try to remeber to do this.

If you are using terminal, run this command: `git checkout -b <branchname> dev`
If you are on github desktop: click the "Current Branch dropdown" (same as step 1), then click "New Branch", and then make sure you create the branch based on dev. See below:
<img width="400" height="409" alt="Screenshot 2025-10-22 at 9 40 02 PM" src="https://github.com/user-attachments/assets/74d70a76-cbdf-4ab7-ac4d-b469e8a6aca6" />
You should automatically be switched onto your new branch

### 3. Commiting and pushing
Commit semi-frequently. After you implement a file or a feature -- or even a sub feature if its a large feature -- then commit. Try to avoid writing a shit ton of code and commiting all at once. If you have a bug you can't fix, it would make it much more costly to rollback to a previous commit. 

You only really need to push if 1. someone wants to hop on your branch and look at your code 2. you are going to make a pull request. Note that on github desktop you will have to publish your branch before pushing.
<img width="653" height="273" alt="Screenshot 2025-10-22 at 9 53 24 PM" src="https://github.com/user-attachments/assets/adea81f4-3f4a-46e3-a38f-1b6706d27cce" />

### 4. Creating pull requests
Once you have working, tested code that you want to push to dev, you will make a pull request. You can do this in github desktop or online. On github desktop, there will be a "Create pull request" button in the same place as the "Publish branch" button above. If you dont use desktop, just go to github online and there should be an option to "compare and pull request."

Add a description to your pull request that breifly describes the features, classes, etc that are on your branch. Make sure you are merging into the correct branch, Most of the time this iwll be dev. See below:
<img width="930" height="669" alt="Screenshot 2025-10-22 at 10 02 18 PM" src="https://github.com/user-attachments/assets/31af3b30-9609-49b1-b738-3bacd09bf7a7" />

If there are no conflicts, it should say "Able to merge." **ONLY CREATE THE PULL REQUEST IF THERE ARE NO CONFLICTS WITH THE BRANCH YOU ARE MERGING INTO.**

### 5. Merging pull requests
NEVER MERGE YOUR OWN PULL REQUESTS (unless you are working last minute and it is the night the project is due).

If you are going to merge someone elses pull request, I would recommend the following:
   i) Go on their branch and do a breif test of the app and their features to make sure nothing is broken.
   ii) If you are too lazy to do step i), at least review the changed files in the pull request and make sure they don't look wack (use your judgment)
   iii) Merge the pull request if the app/features are working AND there are no conflicts. If there are conflicts, refer to step 6.

### 6. Dealing with merge conflicts
If there is a merge conflict with a pull request, DO NOT TRY TO RESOLVE IT YOURSELF. You will want to consult whoever's pull request you are merging. Maybe get on a quick meeting with them and go through the conflicts together. If the conflict is trivial (comments, whitespace, newlines, etc), then you can fix the conflict yourself.

Possibly more on this in the future.

### 7. Merging dev into main
The dev branch will only be merged into main when we have a working version of our app with major features. Merging into main too frequently defeats the purpose of having dev. Idealy we only merge when we have major milestones completed, or a project due.
