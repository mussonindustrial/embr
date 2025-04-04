---
'@embr-modules/periscope-web': minor
'@embr-modules/periscope': minor
---

Add a Web Library for loading JavaScript/CSS/web resource files.

- This update adds a Web Library folder for hosting web content.
- The folder is created under `${data-dir}/com.mussonindustrial.embr.periscope/web-library`
- All files placed in this folder and its subdirectories are available at `/data/periscope/web-library/${path.extension}`
  - For example, the file `/web-library/my-folder/testing.js` would be available at `/data/periscope/web-library/my-folder/testing.js`