THE UNINVITED — GitHub Actions build (experimental source, NOT a tested JAR)

1. Create a new public or private GitHub repository.
2. Upload all project files including .github/workflows/build.yml (GitHub web upload may hide dot folders; easiest is GitHub Desktop).
3. Open Actions > Build THE UNINVITED > Run workflow.
4. If green, open the run and download THE_UNINVITED-jar artifact. Unzip it and use the .jar from build/libs.
5. If red, download the logs and send the FIRST error.

WARNING: Previous ForgeGradle failed on duplicate entry mcp/client/Start.class. This workflow does not guarantee that the upstream issue is fixed. Source code is a preliminary Enderman-based prototype, not yet a custom white-eyed model. Do not install an untested JAR into an important world without a backup.
