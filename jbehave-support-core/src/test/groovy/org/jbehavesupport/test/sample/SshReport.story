Narrative:
This scenario is sample story for ServerLogXmlReporterExteson
In output we can se:
2X LONG_REPORTABLE (file) created from TEMPLATE and FULL mode
2X TEST created from CACHE and FULL mode

Scenario: Test TEMPLATE mode of ssh extension

Given ssh reporter mode is set to [TEMPLATE]

Then the following data are present in [TEST] log:
| header     |
| long       |
| unexpected |

Scenario: Test CACHE mode of ssh extension

Given ssh reporter mode is set to [CACHE]

Then the following data are present in [TEST] log:
| header     |
| long       |
| unexpected |

Scenario: Test FULL mode of ssh extension

Given ssh reporter mode is set to [FULL]

Then the following data are present in [TEST] log:
| header     |
| long       |
| unexpected |