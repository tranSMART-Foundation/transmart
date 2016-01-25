//# sourceURL=boxplotModel.js

"use strict";

window.smartR.BoxplotModel = function() {

    this.name = 'boxplot-model';

    // TODO: use real data
    this.json =  {
        "cohort1": {
            "concept": ["Concept name"],
            "globalMin": [32],
            "globalMax": [65],
            "no subset": {
                "lowerWhisker": [32],
                "lowerHinge": [41],
                "median": [48],
                "upperHinge": [56],
                "upperWhisker": [65],
                "points": [
                    {
                        "patientID": 16708,
                        "value": 62,
                        "jitter": -0.4359,
                        "outlier": false
                    },
                    {
                        "patientID": 16707,
                        "value": 36,
                        "jitter": 0.073,
                        "outlier": false
                    },
                    {
                        "patientID": 16706,
                        "value": 51,
                        "jitter": 0.3043,
                        "outlier": false
                    },
                    {
                        "patientID": 16705,
                        "value": 55,
                        "jitter": 0.2571,
                        "outlier": false
                    },
                    {
                        "patientID": 16704,
                        "value": 48,
                        "jitter": 0.3908,
                        "outlier": false
                    },
                    {
                        "patientID": 16703,
                        "value": 45,
                        "jitter": -0.0522,
                        "outlier": false
                    },
                    {
                        "patientID": 16702,
                        "value": 65,
                        "jitter": -0.3476,
                        "outlier": false
                    },
                    {
                        "patientID": 16701,
                        "value": 56,
                        "jitter": -0.0267,
                        "outlier": false
                    },
                    {
                        "patientID": 4705,
                        "value": 45,
                        "jitter": -0.237,
                        "outlier": false
                    },
                    {
                        "patientID": 4701,
                        "value": 32,
                        "jitter": -0.2669,
                        "outlier": false
                    },
                    {
                        "patientID": 18705,
                        "value": 47,
                        "jitter": 0.0891,
                        "outlier": false
                    },
                    {
                        "patientID": 18704,
                        "value": 48,
                        "jitter": -0.4146,
                        "outlier": false
                    },
                    {
                        "patientID": 18703,
                        "value": 57,
                        "jitter": 0.3524,
                        "outlier": false
                    },
                    {
                        "patientID": 18702,
                        "value": 41,
                        "jitter": -0.0684,
                        "outlier": false
                    },
                    {
                        "patientID": 18701,
                        "value": 39,
                        "jitter": 0.3212,
                        "outlier": false
                    },
                    {
                        "patientID": 16710,
                        "value": 33,
                        "jitter": -0.3884,
                        "outlier": false
                    },
                    {
                        "patientID": 16709,
                        "value": 65,
                        "jitter": 0.0082,
                        "outlier": false
                    }
                ]
            },
            "subsets": ["no subset"]
        }
    };

    return this;
};
