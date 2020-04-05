# Comparison of VISA implementations

I played around with four VISA implementations and recorded my findings. I mostly looked at the UI design and how nice I thought they would be to use in the real world. I didn't put them through any rigorous testing.

Unless you have reason not to, you should probably just install NI-VISA.

The four implementations:

* [Keysight](keysight)
* [National Instruments](national-instruments)
* [Rohde & Schwarz](rohde-and-schwarz)
* [Tektronix](tektronix)

## What's inside?

[According](https://www.tek.com/support/faqs/what-tekvisa-and-how-can-i-use-it-communicate-and-control-my-instrument) to Tektronix,

>Each VISA [implementation] is comprised of a communications driver, a USBTMC driver (USB Test and Measurement Class driver), a VISA software library and documentation, an instrument connection manager, an instrument communication tool, and an instrument communication logger.

## Multiple VISA implementations installed at the same time

Not recommended! It's possible but complicated. Here are two links to get you started.

* [Interoperating With Two VISA Implementations on the Same Computer](https://www.keysight.com/upload/cmc_upload/All/knowledge_code_02_dual_visa_readme.pdf) (pdf)
* [After Installing 3rd Party VISA Software NI VISA No Longer Works](https://knowledge.ni.com/KnowledgeArticleDetails?id=kA00Z0000019KoXSAU&l=en-US)

