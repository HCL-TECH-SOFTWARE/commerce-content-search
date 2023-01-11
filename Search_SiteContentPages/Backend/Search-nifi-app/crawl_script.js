const puppeteer = require('puppeteer');
const fs = require('fs');

async function tutorial() {
   try {
        const URL = process.argv[2];
        const filepath = process.argv[3]+URL.replace(/\//g,'__');
        const browser = await puppeteer.launch({
                   headless: true,
                   args: ['--no-sandbox']
                })
        const page = await browser.newPage()

        await page.goto(URL,{waitUntil: 'networkidle0'});
        const data = await page.evaluate(() => document.getElementsByClassName('page')[0].textContent);
        const html = await page.content();
        await browser.close();
        fs.writeFile(filepath, html, function(err) {
                if(err) {
                        return console.log(err);
                 }

                console.log(filepath);
        });
        fs.chmodSync(filepath,'777');

   } catch (error) {
       console.error(error)
   }
}

tutorial()

