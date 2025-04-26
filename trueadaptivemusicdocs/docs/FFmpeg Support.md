# FFmpeg Support

Some Music Packs may contain audio file types that are non-ogg (e.g. wav or mp3), which are not supported by Minecraft's sound engine. If a pack contains a file like this and you haven't gone through this tutorial yet, you will see this in the pack selection screen:

![Non-ogg error](non_ogg.png)
The pack will still be usable, but any non-ogg files will be skipped. To make these files work you need to install FFmpeg.

## How to install and set up FFmpeg

### Step 1: Open Powershell as Administrator

First open the windows search, search for powershell and click "Open as Administrator". Hit yes for any following dialogs.
![Open powershell](open_powershell.png)

### Step 2: Install FFmpeg

Now run the command `winget install "FFmpeg (Essentials Build)"`. Just type it into the prompt and hit enter.
![ffmpeg install](ffmpeg.png)

### Step 3: Restart Your PC

Finally restart your PC and you should be good to go!
