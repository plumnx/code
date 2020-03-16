local code_current_value = tonumber(redis.call('hget', KEYS[1], KEYS[2]))
local code_limit_value = tonumber(redis.call('hget', KEYS[1], KEYS[3]))

if code_current_value == nil or code_limit_value == nil then
  return nil

else
  if code_current_value + tonumber(ARGV[1]) <= code_limit_value then
    redis.call('hincrby', KEYS[1], KEYS[2], ARGV[1])
    return {1, code_current_value, code_limit_value}
  end
end

return {0, code_current_value, code_limit_value}