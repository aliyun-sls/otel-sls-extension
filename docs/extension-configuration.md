# Extension Configuration

## Profiling

* profiling.enable:
* profiling.single_span.max_time:
* profiling.profile_interval:

## Profiling Policy

```json
[
  {
    "name": "policy-1",
    "type": "root_span",
    "root_span": {
      "name": "GET",
      "attributes": [
        {
          "key": "http.method",
          "value": "GET"
        }
      ]
    }
  },
  {
    "name": "policy-2"
  }
]
```