require 'nokogiri'
require 'digest'
require 'json'

files = ARGV

source_files = []
cwd_prefix = Dir.pwd + '/'

unless files.empty?
    files.each do |filename|
        xml = Nokogiri::XML(File.open(filename).read, 'r')

        source_path = xml.css('sources source')[1].content

        xml.css('classes class').each do |klass|
            klass_path = klass.attr('filename')
            klass_absolute_path = source_path + '/' + klass_path
            klass_repo_path = klass_absolute_path[
                cwd_prefix.length..klass_absolute_path.length
            ]

            klass_file = File.open(klass_absolute_path, 'r')
            klass_file_num_lines = klass_file.each_line.count
            klass_file_md5 = Digest::MD5.hexdigest(klass_file.read)
            klass_file.close

            klass_line_coverage = Array.new(klass_file_num_lines)

            klass.css('lines line').each do |line|
                klass_line_coverage[line.attr('number').to_i - 1] = line.attr('hits').to_i
            end

            source_files.push({
                :name => klass_repo_path,
                :source_digest => klass_file_md5,
                :coverage => klass_line_coverage,
            })
        end
    end
end

payload = {
    :service_job_id => ENV['CIRCLE_JOB_ID'],
    :service_name => 'circleci',
    :source_files => source_files,
}

puts payload.to_json
